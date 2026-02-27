import { fetchBirthRecordById } from '@/app/lib/data';
import PhenotypesAtBirthTable from '@/app/ui/relationship/phenotypes-at-birth-table';
import { RelationshipDetails, SheepDetails } from '@/app/ui/buttons';
import BirthRecordDeleteButton from '@/app/ui/relationship/birth-record-delete-button';
import { notFound } from 'next/navigation';

export default async function BirthRecordPage(props: { params: Promise<{ id: string }> }) {
    const params = await props.params;
    const id = params.id;

    const birthRecord = await fetchBirthRecordById(id);

    if (!birthRecord) {
        notFound();
    }

    return (
        <div>
            <h1 className="text-2xl">Birth Record</h1>

            {/* Id */}
            <div className="flex flex-wrap justify-between mt-4">
                <p>ID: {birthRecord.id}</p>
                <BirthRecordDeleteButton brId={birthRecord.id} />
            </div>

            {/* Relationship Id */}
            <div className="mt-4">
                <p>
                    Relationship
                    <RelationshipDetails relId={birthRecord.parentRelationshipSummary.id} />
                </p>
                <p>
                    Parent 1:
                    <SheepDetails sheepId={birthRecord.parentRelationshipSummary.parent1.id}>
                        <span>{birthRecord.parentRelationshipSummary.parent1.name ?? "(unnamed)"}</span>
                    </SheepDetails>
                </p>
                <p>
                    Parent 2:
                    <SheepDetails sheepId={birthRecord.parentRelationshipSummary.parent2.id}>
                        <span>{birthRecord.parentRelationshipSummary.parent2.name ?? "(unnamed)"}</span>
                    </SheepDetails>
                </p>
            </div>

            {/* Child */}
            <p className="mt-4">
                Child:
                {birthRecord.child ?
                    <SheepDetails sheepId={birthRecord.child.id} className="mt-2">
                        <span>{birthRecord.child.name ?? "(unnamed)"}</span>
                    </SheepDetails>
                    :
                    <span> not saved</span>
                }
            </p>


            {/* Phenotypes at birth */}
            <PhenotypesAtBirthTable birthRecord={birthRecord}/>
        </div>
    );
}
